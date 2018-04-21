<?php

namespace App\Http\Controllers\Product;

use Illuminate\Routing\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Cookie;

/**
 * Class ProductController
 */
class ProductController extends Controller{
    /**
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getList(Request $request) {
        $filter_by_ids = $request->input('ids', false);
        $table = DB::table('products');
        if($filter_by_ids && is_array ($filter_by_ids)){
            $list = $table->get();
            $return_data = [];
            foreach ($filter_by_ids as $item) {
                $first = array_first($list, function ($value, $key) use ($item,$list){
                    return $value->id == $item;
                });

                if($first){
                    $return_data[] = $first;
                }

            }

        } else {
            $return_data = $table->get();
        }
        return response()->json( $return_data );
    }
    /**
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function create(Request $request) {
        $data                  = $request->input();
        $id = DB::table('products')->insertGetId( $data );
        if($id ){
            $return_data = ["id"=>$id ];
            return response()->json($return_data);
        }
        return response("Missing Data", 400);
    }
    /**
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function details(Request $request, $id) {
        $item = DB::table('products')->where('id', $id)->first();
        if($item){
            $cookie_products = $request->cookie('last_visited');
            $products = $cookie_products ? explode(",",$cookie_products):[];
            array_unshift($products,$id);
            $unique = array_unique($products);
            $last_visited = array_slice($unique, 0, 5, true);
            $products_string = implode(",", $last_visited);
            $cookie = cookie('last_visited', $products_string, $minutes = 0, $path = null, $domain = null, $secure = false, $httpOnly = false);
            $last_visited_array = [];
            foreach ($last_visited as $v_product) {
                $last_visited_array[] = $v_product;
            }
            return response()->json([
                "product"=>$item,
                "last_visited"=> $last_visited_array,
            ])->withCookie($cookie);
        }
        return response("Missing Data", 400);
    }

    /**
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $user_id) {
        $data                  = $request->input();

        $return_data = [];
        return response()->json($return_data);
    }


}